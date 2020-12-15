package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.DataContract
import com.template.states.DataState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********

var index = 1

@InitiatingFlow
@StartableByRPC
class MyFlow(val dataString: String) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        // TODO: HEATH CHECK (through webserver?)

        val destParty = getNextNode()

        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val command = Command(DataContract.Commands.Create(), listOf(ourIdentity).map { it.owningKey })
        val dataState = DataState(dataString, ourIdentity, destParty)
        val stateAndContract = StateAndContract(dataState, DataContract.ID)
        val txBuilder = TransactionBuilder(notary).withItems(stateAndContract, command)

        txBuilder.verify(serviceHub)

        val tx = serviceHub.signInitialTransaction(txBuilder)
        val targetSession = initiateFlow(destParty)
        return subFlow(FinalityFlow(tx, targetSession))
    }



    // TODO: get configs / party map once and store in flow context ??? (for performance)
    fun getNextNode(): Party {
        // Get values from CorDapp Config
        val config = serviceHub.getAppContext().config
        val range = config.getInt("range")
        val name = config.getString("name")

        // Construct X500 Name
        val x500Name = CordaX500Name.parse("OU=" + index + "," + name)
        val party = serviceHub.networkMapCache.getPeerByLegalName(x500Name) ?: throw FlowException("No peer found with X500 name " + x500Name)

        // Increment counter
        if (++index > range) index = 1

        return party
    }

    /*
    fun getNodeList(): List<Party> {
        val config = serviceHub.getAppContext().config

        val range = config.getInt("range")
        val name = config.getString("name")

        val partyList = ArrayList<Party>()
        for(i in 1..range) {
            val newName = "OU=" + i + "," + name
            val party = serviceHub.networkMapCache.getPeerByLegalName(CordaX500Name.parse(newName)) ?: throw FlowException("No peer found with X500 name " + newName)
            partyList.add(party)
        }
        return partyList
    }
     */



}

@InitiatedBy(MyFlow::class)
class MyFlowResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {

        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}

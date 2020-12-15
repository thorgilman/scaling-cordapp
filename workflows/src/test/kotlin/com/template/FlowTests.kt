package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.flows.MyFlow
import com.template.flows.MyFlowResponder
import com.template.states.DataState
import com.typesafe.config.ConfigFactory
import net.corda.core.contracts.ContractState
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test


class FlowTests {

    lateinit var mockNetwork: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b1: StartedMockNode
    lateinit var b2: StartedMockNode
    lateinit var b3: StartedMockNode
    lateinit var cordappConfig: Map<String, String>

    @Before
    fun setup() {

        cordappConfig = getCorDappConfig("workflows-0.1.conf")

        mockNetwork = MockNetwork(MockNetworkParameters((listOf(TestCordapp.findCordapp("com.template.flows").withConfig(cordappConfig),
                                                                TestCordapp.findCordapp("com.template.contracts")))))

        a = mockNetwork.createNode(CordaX500Name.parse("O=MyParty,L=New York,C=US"))
        b1 = mockNetwork.createNode(CordaX500Name.parse("OU=1,O=CentralNode,L=New York,C=US"))
        b2 = mockNetwork.createNode(CordaX500Name.parse("OU=2,O=CentralNode,L=New York,C=US"))
        b3 = mockNetwork.createNode(CordaX500Name.parse("OU=3,O=CentralNode,L=New York,C=US"))

        val startedNodes = arrayListOf(a, b1, b2, b3)
        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach { it.registerInitiatedFlow(MyFlowResponder::class.java) }
        mockNetwork.runNetwork()
    }

    @After
    fun tearDown() {

    }


    @Test
    fun `test`() {
        for (x in 1..5) {
            val future = a.startFlow(MyFlow("Data"))
            mockNetwork.runNetwork()
            val tx = future.getOrThrow()
        }
    }


    private fun StartedMockNode.identity(): Party {
        return this.info.legalIdentities[0]
    }

    private inline fun <reified T: ContractState> StartedMockNode.getStates(): List<ContractState> {
        return services.vaultService.queryBy<T>().states.map{it.state.data}
    }

    private fun getCorDappConfig(configFileName: String): Map<String, String> {
        val cordappConfig: MutableMap<String, String> = LinkedHashMap()
        val config = ConfigFactory.parseResources(configFileName)
        for ((key) in config.entrySet()) {
            cordappConfig[key] = config.getString(key)
        }
        return cordappConfig
    }

}
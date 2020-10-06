import { useState } from 'react';
import React from 'react';
import urls from "../services/urls";
import http from "../services/http";
import '../styling/Button.scss';
import '../styling/NetworkInfo.css';
import { SHOW_NETWORK_INFO, HIDE_NETWORK_INFO} from "../services/buttons";

function NetworkInfo() {
    const [buttonText, setButtonText] = useState(SHOW_NETWORK_INFO)
    const [shouldDisplayTable, setDisplayTable] = useState(false)
    const [networkParameters, setNetworkParameters] = useState([])

    const changeText = (text) => {
        console.log("LENGTH")
        console.log(networkParameters.whitelistedContractImplementations)
        // console.log(networkParameters.whitelistedContractImplementations.length)
        setButtonText(text)
        setDisplayTable(!shouldDisplayTable)
    }

    const getButtonText = () => shouldDisplayTable ? SHOW_NETWORK_INFO : HIDE_NETWORK_INFO

    function getNetworkParameters() {
        http.get(urls.get_network_params)
            .then(r => {
                if(r.status === 200 && r.data.status === true){
                    // console.log(r.data.data)
                    setNetworkParameters(r.data.data)

                } else {
                }
            });
    }

    return (
        <div>
            {/*eslint-disable-next-line*/}
            <a type="button"
               className="btn btn-2"
               onClick={ () => { getNetworkParameters(); changeText( getButtonText() )}}>{buttonText}</a>
            { shouldDisplayTable &&
                <div className="net-params">
                    <div className="network-wrapper">
                        <div className="param-info">
                            <div className="b">Network Parameters</div>
                            <div><span>Minumum Platform Version: </span>{networkParameters.minimumPlatformVersion}</div>
                            <div><span>Last Modified: </span> {networkParameters.modifiedTime}</div>
                            <div><span>Max Transaction Size: </span> {networkParameters.maxTransactionSize/(1024 * 1024)} MB</div>
                            <div style={{marginTop: 10}}>
                                <div><strong>Notaries</strong></div>
                                {
                                    networkParameters.notaries && networkParameters.notaries.length > 0?
                                        networkParameters.notaries.map((notary, index) => {
                                            return (
                                                <div key={index} className="appInfo-wrapper">
                                                    <div className="param-info" style={{marginRight: index%2===0?5:0, marginLeft: index%2===0?0:5, marginTop: 5}}>
                                                        <div><span>Name: </span>{notary.identity}</div>
                                                        <div><span>Type: </span> {notary.validating?'Validating':'Non-Validating'}</div>
                                                    </div>
                                                </div>
                                            )
                                        }): <div style={{padding: "10px 0"}}>No Notaries Found</div>
                                }
                            </div>

                            <div style={{marginTop: 10}}>
                                <div><strong>Whitelisted Contracts</strong></div>
                                {
                                    networkParameters.whitelistedContractImplementations ?
                                        Object.keys(networkParameters.whitelistedContractImplementations).map((contract, index) => {
                                            return (
                                                <div key={index} className="appInfo-wrapper" style={{width: "100%"}}>
                                                    <div className="param-info" style={{marginTop: 5}}>
                                                        <div><span>Contract: </span>{contract}</div>
                                                        <div><span>Hash: </span> {networkParameters.whitelistedContractImplementations[contract]}</div>
                                                    </div>
                                                </div>
                                            )
                                        }): <div style={{padding: "10px 0"}}>No Whitelisted Contracts Found</div>
                                }
                            </div>

                        </div>
                    </div>

                </div>
            }
        </div>
    );
}
export default NetworkInfo;

import com.sap.piper.GitUtils
import groovy.transform.Field

import com.sap.piper.ConfigurationMerger
import com.sap.piper.cm.ChangeManagement
import com.sap.piper.cm.ChangeManagementException

import hudson.AbortException


@Field def STEP_NAME = 'transportRequestGetTransports'

@Field Set parameterKeys = [
    'changeId',
    'cmCredentialsId',
    'cmEndpoint'
  ]

@Field Set stepConfigurationKeys = [
    'cmCredentialsId',
    'cmEndpoint'
  ]

def call(parameters = [:]) {

    handlePipelineStepErrors (stepName: STEP_NAME, stepParameters: parameters) {

        def script = parameters?.script ?: [commonPipelineEnvironment: commonPipelineEnvironment]

        ChangeManagement cm = new ChangeManagement(script)

        Map configuration = ConfigurationMerger.merge(parameters.script, STEP_NAME,
                                                      parameters, parameterKeys,
                                                      stepConfigurationKeys)

        def changeId = configuration.changeId
        if(!changeId) throw new AbortException('Change id not provided (parameter: \'changeId\').')

        def cmCredentialsId = configuration.cmCredentialsId
        if(!cmCredentialsId) throw new AbortException('Credentials id not provided (parameter: \'cmCredentialsId\').')

        def cmEndpoint = configuration.cmEndpoint
        if(!cmEndpoint) throw new AbortException('Solution Manager endpoint not provided (parameter: \'cmEndpoint\').')

        def transportRequestIds

        echo "[INFO] Getting transport requests for change document '$changeId'."

        withCredentials([usernamePassword(
            credentialsId: cmCredentialsId,
            passwordVariable: 'password',
            usernameVariable: 'username')]) {

            try {
                transportRequestIds = cm.getTransportRequests(changeId, cmEndpoint, username, password)
            } catch(ChangeManagementException ex) {
                throw new AbortException(ex.getMessage())
            }
        }

        echo "[INFO] The following transport requests $transportRequestIds are available for change document '$changeId'."
        return transportRequestIds
    }
}

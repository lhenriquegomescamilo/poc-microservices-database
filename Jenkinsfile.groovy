// This step should not normally be used in your script. Consult the inline help for details.
podTemplate(
        label: 'poc-microservices',
        namespace: 'devops',
        name: 'poc-microservices',
        containers: [
                containerTemplate(alwaysPullImage: true, args: 'cat', command: '/bin/sh -c', envVars: [], image: 'docker', livenessProbe: containerLivenessProbe(execArgs: '', failureThreshold: 0, initialDelaySeconds: 0, periodSeconds: 0, successThreshold: 0, timeoutSeconds: 0), name: 'docker-container', ports: [], privileged: false, resourceLimitCpu: '', resourceLimitMemory: '', resourceRequestCpu: '', resourceRequestMemory: '', shell: null, ttyEnabled: true, workingDir: '/home/jenkins'),
                containerTemplate(alwaysPullImage: true, args: 'cat', command: '/bin/sh -c', image: 'lachlanevenson/k8s-helm:v2.11.0', name: 'helm-container', ttyEnabled: true)
        ],
        volumes: [hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')]) {
    node('poc-microservices') {
        def REPOSITORY
        def GIR_URL_REPOSITORY = 'git@github.com:lhenriquegomescamilo/poc-microservices-database.git'
        def DOCKER_IMAGE = "gateway_database"
        def DOCKER_IMAGE_VERSION = ""
        def MICROSERVICE_NAME = "database"
        def KUBE_NAMEPSACE = ""
        def ENVIRONMENT = "dev"
        def GIT_BRANCH
        def REPO_HELM_NAME = "poc-microservice"
        def HELM_SERVICE_CHARMUSEUM_URL = "http://helm-chartmuseum:8080"
        def HELM_DEPLOY_NAME
        def HELM_CHART_NAME = "${REPO_HELM_NAME}/${MICROSERVICE_NAME}"

        stage('Checkout') {
            echo 'Iniciando clone do Repositorio'
            REPOSITORY = checkout([$class: 'GitSCM', branches: [[name: '*/master'], [name: '*/dev']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'github', url: GIR_URL_REPOSITORY]]])
            echo REPOSITORY.toString()
            GIT_BRANCH = REPOSITORY.GIT_BRANCH
            if (GIT_BRANCH.equals("origin/master")) {
                KUBE_NAMEPSACE = "prod"
                ENVIRONMENT = 'prod'
                HELM_DEPLOY_NAME = ENVIRONMENT+"-"+MICROSERVICE_NAME
            } else if (GIT_BRANCH.equals("origin/dev")) {
                KUBE_NAMEPSACE = "development"
                ENVIRONMENT = 'development'
                HELM_DEPLOY_NAME = ENVIRONMENT+"-"+MICROSERVICE_NAME
            } else {
                echo "Não existe pipeline para a branch ${GIT_BRANCH}"
                exit 0
            }
            sh "ls -ltra"
            DOCKER_IMAGE_VERSION = sh label: 'get version', returnStdout: true, script: 'sh read-package-json-version.sh'
            DOCKER_IMAGE_VERSION = DOCKER_IMAGE_VERSION.trim()
            echo "EXIBINDO DOCKER IMAGE VERSION ${DOCKER_IMAGE_VERSION}"

        }

        stage('Package') {
            // This step should not normally be used in your script. Consult the inline help for details.
            container('docker-container') {
                withCredentials([usernamePassword(credentialsId: 'docker-hub', passwordVariable: 'DOCKER_HUB_PASSWORD', usernameVariable: 'DOCKER_HUB_USER')]) {
                    echo 'Building com npm repositorio'
                    sh "docker login -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PASSWORD}"
                    sh "docker build -t ${DOCKER_HUB_USER}/${DOCKER_IMAGE}:${DOCKER_IMAGE_VERSION} ."
                    sh "docker push ${DOCKER_HUB_USER}/${DOCKER_IMAGE}:${DOCKER_IMAGE_VERSION}"
                }
            }
        }

        stage('Deploy') {
            container('helm-container') {
                echo 'Deploy com helm'
                sh "helm init --client-only"
                sh "helm repo add ${REPO_HELM_NAME} ${HELM_SERVICE_CHARMUSEUM_URL}"
                sh "helm repo update"
                sh "helm repo list"
                sh "helm search ${REPO_HELM_NAME}"

                try {
                    sh "helm upgrade --namespace=${KUBE_NAMEPSACE} ${HELM_DEPLOY_NAME} ${HELM_CHART_NAME} --set image.tag=${DOCKER_IMAGE_VERSION}"
                } catch (Exception e) {
                    sh "helm install --namespace=${KUBE_NAMEPSACE} --name=${HELM_DEPLOY_NAME} ${HELM_CHART_NAME} --set image.tag=${DOCKER_IMAGE_VERSION}"
                }

            }
        }
    } // end of node
}

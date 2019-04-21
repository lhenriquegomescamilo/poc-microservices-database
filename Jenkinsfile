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
        def GIR_URL_REPOSITORY = 'git@github.com:lhenriquegomescamilo/poc-microservices.git'
        def DOCKER_IMAGE = "gateway_database"
        def DOCKER_IMAGE_VERSION = ""
        def MICROSERVICE_NAME = "database"

        def REPO_HELM_NAME = "poc-microservice"
        def HELM_SERVICE_CHARMUSEUM_URL = "http://helm-chartmuseum:8080"

        stage('Checkout') {
            echo 'Iniciando clone do Repositorio'
            REPOSITORY = git credentialsId: 'github', url: GIR_URL_REPOSITORY
            echo REPOSITORY.toString()
            dir(MICROSERVICE_NAME) {
                sh "ls -ltra"
                DOCKER_IMAGE_VERSION = sh label: 'get version', returnStdout: true, script: 'sh read-package-json-version.sh'
                DOCKER_IMAGE_VERSION = DOCKER_IMAGE_VERSION.trim()
                echo "EXIBINDO DOCKER IMAGE VERSION ${DOCKER_IMAGE_VERSION}"
            }
        }

        stage('Package') {
            // This step should not normally be used in your script. Consult the inline help for details.
            container('docker-container') {
                withCredentials([usernamePassword(credentialsId: 'docker-hub', passwordVariable: 'DOCKER_HUB_PASSWORD', usernameVariable: 'DOCKER_HUB_USER')]) {
                    echo 'Building com npm repositorio'
                    sh "docker login -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PASSWORD}"
                    sh "docker build -t ${DOCKER_HUB_USER}/${DOCKER_IMAGE}:${DOCKER_IMAGE_VERSION} ./${MICROSERVICE_NAME}"
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
                sh "helm upgrade ${MICROSERVICE_NAME} ${REPO_HELM_NAME}/${MICROSERVICE_NAME} --set image.tag=${DOCKER_IMAGE_VERSION}"

            }
        }
    } // end of node
}

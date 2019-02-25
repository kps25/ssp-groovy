#!/usr/bin/groovy
def settings() {
    [
            'kubectlImage' : 'ssp25/ssp-kubectl:v1',
            'docker_build_image' : 'node:carbon',
            'imageName'          :  'ssp25/ssp-nodejs'
    ]
}


def packageImage()
{
  def settings = settings()
  stage('Package Image') {
    def image = docker.build(settings.imageName)
        image.push(branchTag())
        image.push(branchAndBuildTag())
      }
}

def deployImage(context, namespace)
{
  def settings = settings()
  stage('Deploy Image') {
      docker.image(settings.kubectlImage).inside {
      deploymentUpdateArtisanMobileBff("${context}", "${namespace}", "${settings.imageName}:${branchAndBuildTag()}")
      }
  }
}

def branchAndBuildTag() {
    return "${env.BRANCH_NAME}${env.BUILD_NUMBER}"
}
def branchTag() {
    return "${env.BRANCH_NAME}"
}


def envMap() {
    [
      'dev' : [context: 'dev.sspcloudpro.co.in', namespace: 'dev', branch: 'develop', cluster: 'dev.sspcloudpro.co.in', url: 'dev.sspcloudpro.co.in']
    ]
}


def branchMap(branch) {
    def bMap = [
          develop: ['dev'],
          release: ['int'],
    ]
    return bMap."${branch}"
}


// Deploy Mobile
def deploymentUpdateArtisanMobileBff(context, namespace, newVersion) {
    def cmd = "kubectl set image deployment/ssp-nodejs-deployment ssp-nodejs-deployment=${newVersion} --context=${context} --namespace=${namespace}"



    sh cmd
}


return this;

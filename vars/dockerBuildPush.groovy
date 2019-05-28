// vars/dockerBuildPush.groovy
def call(String imageName, String imageTag = env.BUILD_NUMBER, String target = ".", String dockerFile="Dockerfile", Closure body) {
  def dockerReg = "gcr.io/core-workshop"
  imageName = "helloworld-nodejs"
  def repoName = env.repoOwner + "-" + imageName
  repoName = repoName.toLowerCase()
  def label = "kaniko"
  def podYaml = libraryResource 'podtemplates/dockerBuildPush.yml'
  podTemplate(name: 'kaniko', label: label, yaml: podYaml) {
    node(label) {
      imageNameTag()
      gitShortCommit()
      container(name: 'kaniko', shell: '/busybox/sh') {
        body()
        withEnv(['PATH+EXTRA=/busybox:/kaniko']) {
          sh """#!/busybox/sh
            executor -f ${pwd()}/${dockerFile} -c ${pwd()} --build-arg context=${env.IMAGE_REPO}-${imageName} --build-arg buildNumber=${BUILD_NUMBER} --build-arg shortCommit=${SHORT_COMMIT} --build-arg commitAuthor=${COMMIT_AUTHOR} -d ${dockerReg}/${repoName}:${BUILD_NUMBER}
          """
        }
        }
      }
    }
}

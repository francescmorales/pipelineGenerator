import groovy.io.FileType

def listOfFiles = []

//Separate folders
def separator = "/"
def folders = pipelinePath.split(separator)

//Environments list
def envs = ["DEV","PRE","PRO","UAT"]

// Create folders for the path
def folderName
def firstFolder = true
folders.each{ dir ->
	if(firstFolder){
		folderName = dir
		folder(folderName)
		firstFolder = false
	}else{
		folderName = folderName + separator + dir
		folder(folderName)
	}
}
def pipelineFolderName = folderName + separator + pipelineName
folder(pipelineFolderName){
	primaryView("DEV")
}

//generate jobName
def jobName = pipelineFolderName + separator + pipelineName

//Generate properties file
new File("/var/jenkins_home/workspace/${pipelineFolderName}/properties").mkdirs()  
def propsFile = new File("/var/jenkins_home/workspace/${pipelineFolderName}/properties/config.properties")
propsFile.write("gitURL=${gitURL}\npipelineType=${pipelineType}\npipelineName=${pipelineName}\npipelinePath=${pipelinePath}\nslavePath=/home/jenkins/workspace/${jobName}")

//Create jobs for diferents envs
envs.each{ env ->
  
  //Create view
  listView("${pipelineFolderName}/${env}") {
      jobs {
        name("${pipelineName}-${env}")
      }
      columns {
          status()
          weather()
          name()
          lastSuccess()
          lastFailure()
          lastDuration()
          buildButton()
      }
  }
  
  // Time to create Job.
  pipelineJob("${jobName}-${env}") {
      definition {
          cpsScm {
              scm{
                  git{
                      branch("*/develop")
                      remote{
                          url("https://github.com/francescmorales/jenkinsfiles.git")
                      }
                  }
              }
              scriptPath("${pipelineType}/jenkinsfile")
          }
      }
  }
  
  //Run new pipeline
  queue("${jobName}-${env}")
}
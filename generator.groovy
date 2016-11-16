import groovy.io.FileType

def listOfFiles = []

//Separate folders
def separator = "/"
def folders = pipelinePath.split(separator)

//Environments list
def envs = ["DEV","PRE","PRO","UAT"]

//Environments parameters
def envVarsMap = this.binding.variables

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
def properties = ""
envVarsMap.each{ envKey,envValue ->
	properties = properties + "${envKey}=${envValue}\n"
}
new File("/var/jenkins_home/workspace/${pipelineFolderName}/properties").mkdirs()  
def propsFile = new File("/var/jenkins_home/workspace/${pipelineFolderName}/properties/config.properties")
propsFile.write(properties)

//Create jobs for diferents envs
if(pipelineType.equals("Dockerfiles")){

	// Time to create Job.
  generatePipelineJob(jobName, pipelineType)
  
  //Run new pipeline
  queue("${jobName}-DOCKER")
}else{

	envs.each{ env ->
	  
	  //Create view
	  generateListViewJob(pipelineFolderName, pipelineName, env)
	  
	  // Time to create Job.
	  generatePipelineJob(jobName, pipelineType, env)
	}
}
	
//Run new pipeline
  queue("${jobName}-DEV")
  
//---------------------FUNCTIONS---------------------//

//generate pipeline job
def generatePipelineJob(String jobName, String pipelineType, String env = "DOCKER"){

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
}

//generate list view job
def generateListViewJob(String pipelineFolderName, String pipelineName, String env){
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
}
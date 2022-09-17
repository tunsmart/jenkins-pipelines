timestamps {

node { 
    
    def mavenHome = tool name: 'Maven'
	stage ('tesla-app - Checkout') {
 	 checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'GithubCredentials', url: 'https://github.com/tunsmart/maven-web-application']]]) 
	}
	stage ('tesla-app - Build') {
 	
        sh "${mavenHome}/bin/mvn clean install"      
	}
	stage ('SonarQube - Report') {
	    withSonarQubeEnv ('SonarqubeServer') {
             sh "${mavenHome}/bin/mvn sonar:sonar"
	    }
	}
}

stage("Quality Gate"){
  timeout(time: 1, unit: 'HOURS') { // Just in case something goes wrong, pipeline will be killed after a timeout
    def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
    if (qg.status != 'OK') {
      error "Pipeline aborted due to quality gate failure: ${qg.status}"
    }
    else {
        print "Sonarqube quality check was succesful."
    }
  }
}
}

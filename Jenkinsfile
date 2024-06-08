node {
    stage('SCM') {
        git branch: 'main', credentialsId: 'github-account', url: 'https://github.com/for-Ely/OOPee.git'
    }
    stage('SonarQube Analysis') {
        def scannerHome = tool 'SonarQube Scanner';
            withSonarQubeEnv() {
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.java.binaries=. -Dsonar.projectKey=PJ1-OOPee -Dsonar.login=sqp_9c4ae23a772129427bccace3ffbd83016373008d"
}
}
}

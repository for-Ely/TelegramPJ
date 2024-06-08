node {
    stage('SCM') {
        git branch: 'main', credentialsId: 'github-account', url: 'https://github.com/for-Ely/TelegramPJ.git'
    }
    stage('SonarQube Analysis') {
        def scannerHome = tool 'SonarQube Scanner';
            withSonarQubeEnv() {
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.java.binaries=. -Dsonar.projectKey=pj1-telegram -Dsonar.login=sqp_9c4ae23a772129427bccace3ffbd83016373008d"
}
}
}

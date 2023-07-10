void call(pattern, target){
    node ("agent") {
        unstash "workspace"
        def server = Artifactory.newServer url: config.url, credentialsId: config.creds_id
        def uploadSpec = """{
            "files": [
                {
                    "pattern": "${pattern}",
                    "target": "${target}"
                }
            ]
        }"""
        server.upload(uploadSpec)
    }
}
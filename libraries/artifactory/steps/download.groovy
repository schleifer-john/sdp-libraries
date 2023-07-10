void call(pattern,target,flat){
    node ("agent") {
        def server = Artifactory.newServer url: config.url, credentialsId: config.creds_id
        def downloadSpec = """{
            "files": [
                {
                    "pattern": "${pattern}",
                    "target": "${pwd()}/${target}",
                    "flat": "${flat}"
                }
            ]
        }"""
        server.download(downloadSpec)
    }
}

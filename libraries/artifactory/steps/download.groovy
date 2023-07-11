package libraries.artifactory.steps

void call(pattern,target,flat){

    def stashOptions = config?.stashOptions ?: [] as String[]
    validateConfig(config)

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

        runStashCommand(stashOptions);
    }
}

/**
 * Method executed if there are stash options specified.
 * Will build the 'stash' command and execute it, throwing any exceptions that occur.
 * The options and default values are set based on the Jenkins 'stash' documentation.
 * 
 * @param stashOptions the stash options specified in the pipeline configuration
 */
void runStashCommand(stashOptions) {
    if(stashOptions) {
        try {
            // set default values for optional values
            def excludes = stashOptions.excludes ?: ''
            def includes = stashOptions.includes ?: ''

            // Need to use containsKey checks for the default boolean values
            def allowEmpty = stashOptions.containsKey('allowEmpty') ? stashOptions.allowEmpty : false
            def useDefaultExcludes = stashOptions.containsKey('useDefaultExcludes') ? stashOptions.useDefaultExcludes : true

            println ("Executing command [stash name: ${stashOptions.name}, allowEmpty: ${allowEmpty}, excludes: ${excludes}, "
                + "includes: ${includes}, useDefaultExcludes: ${useDefaultExcludes}]")
            stash (name: stashOptions.name, allowEmpty: allowEmpty, excludes: excludes, includes: includes, 
                useDefaultExcludes: useDefaultExcludes)
        }
        catch (any) {
            throw any
        }
    }
}

void validateConfig(config) {

    // Checking to make sure required fields are present (may be redundant once a library_config.groovy is added)
    ArrayList requiredFields = ['url', 'creds_id']
    String missingRequired = ''
    requiredFields.each { field ->
        if (!config.containsKey(field)) {
            missingRequired += "Missing required configuration option: ${field} for the Artifactory download step\n"
        }
    }

    // Check that the name is specified if stashOptions are used
    if(config.containsKey('stashOptions') && !config.stashOptions.containsKey('name')) {
        missingRequired += "Missing required configuration option: stashOptions.name for the Artifactory download step\n"
    }

    if (missingRequired) {
        error missingRequired
    }
}

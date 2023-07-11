package libraries.artifactory

public class DownloadSpec extends JTEPipelineSpecification {
    def Download = null

    LinkedHashMap minimalUnitTestingConfig = [
        url: 'test-url', 
        creds_id: 'test-id'
    ]    

    def setup() {
        Download = loadPipelineScriptForStep('artifactory', 'download')

        explicitlyMockPipelineVariable('out')

        Download.getBinding().setVariable('config', minimalUnitTestingConfig)

        // explicity mock the Artifactory libraries
        explicitlyMockPipelineVariable('Artifactory')
        explicitlyMockPipelineVariable('ArtifactoryServer')

        1 * getPipelineMock("pwd")() >> { return "test-workspace" }

        // mock the Artifactory Factory object's newServer call
        1 * getPipelineMock("Artifactory.newServer")(_) >> { return ArtifactoryServer }
    }

    def 'download request is properly formed' () {
        setup:
            // define the expected result and a placeholder for the actual result
            def expectedDownloadSpec = """{
                "files": [
                    {
                        "pattern": "SAMPLE-REPO/sample/artifact.zip",
                        "target": "test-workspace/libraries",
                        "flat": "true"
                    }
                ]
            }"""
            def result
        when:
            Download("SAMPLE-REPO/sample/artifact.zip", "libraries", true)
        then:
            // mock the Artifactory Server's download method and capture the argument
            1 * getPipelineMock("ArtifactoryServer.download")(_) >> { args ->
                result = args[0]
            }
        expect:
            // trim the whitespace in the result
            assert expectedDownloadSpec.replaceAll("\\s","") == result.replaceAll("\\s","")
    }

    def 'Stash command runs with correct required and default values when minimal stashOptions are specified' () {
        setup:
            Download.getBinding().setVariable('config', [
                stashOptions: [
                    name: 'test-stash'
                ]
            ])
        when:
            Download("SAMPLE-REPO/sample/artifact.zip", "libraries", true)
        then:
            1 * getPipelineMock('stash')(_) >> { args ->
                assert 'test-stash' == args[0].name
                assert false == args[0].allowEmpty
                assert '' == args[0].excludes
                assert '' == args[0].includes
                assert true == args[0].useDefaultExcludes
            }
    }

    def 'Stash command runs when stashOptions are specified' () {
        setup:
            Download.getBinding().setVariable('config', [
                stashOptions: [
                    name: 'test-stash',
                    allowEmpty: true,
                    excludes: 'src/test',
                    includes: 'src/main',
                    useDefaultExcludes: false
                ]
            ])            
        when:
            Download("SAMPLE-REPO/sample/artifact.zip", "libraries", true)
        then:
            1 * getPipelineMock('stash')(_) >> { args ->
                assert 'test-stash' == args[0].name
                assert true == args[0].allowEmpty
                assert 'src/test' == args[0].excludes
                assert 'src/main' == args[0].includes
                assert false == args[0].useDefaultExcludes
            }
    }
}

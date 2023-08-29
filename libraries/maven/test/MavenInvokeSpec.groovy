/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.maven

public class MavenInvokeSpec extends JTEPipelineSpecification {
    def MavenInvoke = null

    LinkedHashMap minimalUnitTestingConfig = [
        unit_test: [
            stageName: 'Maven Unit Tests',
            buildContainer: 'mvn',
            phases: ['test']
        ]
    ]

    static class DummyException extends RuntimeException {
        DummyException(String _message) { super( _message ) }
    }

    def setup() {
        LinkedHashMap config = [:]
        LinkedHashMap env = [:]
        LinkedHashMap stepContext = [ name: 'unit_test' ]

        MavenInvoke = loadPipelineScriptForStep('maven', 'maven_invoke')

        // Redirect to System.out for troubleshooting purposes
        MavenInvoke.getBinding().setProperty('out', System.out)
        // otherwise mock to prevent println errors
        //explicitlyMockPipelineVariable('out')
        explicitlyMockPipelineStep('')
        

        MavenInvoke.getBinding().setVariable('config', config)
        MavenInvoke.getBinding().setVariable('env', env)
        MavenInvoke.getBinding().setVariable('stepContext', stepContext)
    }

    def 'Completes a mvn test successfully' () {
        setup:
            MavenInvoke.getBinding().setVariable('config', minimalUnitTestingConfig)
        when:
            MavenInvoke()
        then:
            1 * getPipelineMock('sh').call('mvn test ')
    }

    def 'Application environment settings take precendence over library config' () {
        setup:
            MavenInvoke.getBinding().setVariable('config', [
                unit_test: [
                    stageName: 'LibConfig Maven Stage',
                    buildContainer: 'mvn',
                    phases: ['clean', 'build']
                ]
            ])
        when:
            MavenInvoke([
                maven: [
                    unit_test: [
                        stageName: 'AppEnv Defined Maven Stage',
                        options: ['-v'],
                        phases: []
                    ]
                ]
            ])
        then:
            MavenInvoke.getBinding().variables.env.buildContainer == 'mvn'
            MavenInvoke.getBinding().variables.env.options == ['-v']
            MavenInvoke.getBinding().variables.env.stageName == 'AppEnv Defined Maven Stage'
            MavenInvoke.getBinding().variables.env.phases == []
    }

    def 'Artifacts get archived as expected' () {
        setup:
            MavenInvoke.getBinding().setVariable('stepContext', [name: 'build'])
            MavenInvoke.getBinding().setVariable('config', [
                build: [
                    stageName: 'Maven Build',
                    buildContainer: 'mvn',
                    phases: ['clean', 'install'],
                    artifacts: ['target/*.jar']
                ]
            ])
        when:
            MavenInvoke()
        then:
            1 * getPipelineMock('archiveArtifacts.call')(_ as Map)
    }

    def 'Stash command runs with correct required and default values when minimal stashOptions are specified' () {
        setup:
            MavenInvoke.getBinding().setVariable('stepContext', [name: 'build'])
            MavenInvoke.getBinding().setVariable('config', [
                build: [
                    stageName: 'Maven Build',
                    buildContainer: 'mvn',
                    phases: ['clean', 'install'],
                    stashOptions: [
                        name: 'test-stash'
                    ]
                ]
            ])            
        when:
            MavenInvoke()
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
            MavenInvoke.getBinding().setVariable('stepContext', [name: 'build'])
            MavenInvoke.getBinding().setVariable('config', [
               build: [
                    stageName: 'Maven Build',
                    buildContainer: 'mvn',
                    phases: ['clean', 'install'],
                    stashOptions: [
                        name: 'test-stash',
                        allowEmpty: true,
                        excludes: 'src/test',
                        includes: 'src/main',
                        useDefaultExcludes: false
                    ]
                ]
            ])            
        when:
            MavenInvoke()
        then:
            1 * getPipelineMock('stash')(_) >> { args ->
                assert 'test-stash' == args[0].name
                assert true == args[0].allowEmpty
                assert 'src/test' == args[0].excludes
                assert 'src/main' == args[0].includes
                assert false == args[0].useDefaultExcludes
            }
    }

    def 'Verify validation check works if required fields are missing' () {
        setup:
            MavenInvoke.getBinding().setVariable('stepContext', [name: 'build'])
            MavenInvoke.getBinding().setVariable('config', [
                build: [:]
            ])
        when:
            MavenInvoke()
        then:
            1 * getPipelineMock('error')(_) >> { args ->
                def lines = args[0].split('\n')
                assert 2 == lines.size()
                assert 'Missing required configuration option: stageName for step: build' == lines[0]
                assert 'Missing required configuration option: buildContainer for step: build' == lines[1]
            }
    }      

    def 'Verify validation check works if stashOptions is missing the required name' () {
        setup:
            MavenInvoke.getBinding().setVariable('stepContext', [name: 'build'])
            MavenInvoke.getBinding().setVariable('config', [
                build: [
                    stageName: 'Maven Build',
                    buildContainer: 'mvn',
                    phases: ['clean', 'install'],
                    stashOptions: [:]
                ]
            ])
        when:
            MavenInvoke()
        then:
            1 * getPipelineMock('error')(_) >> { args ->
                def lines = args[0].split('\n')
                assert 1 == lines.size()
                assert 'Missing required configuration option: stashOptions.name for step: build' == lines[0]
            }
    }    
}

package libraries.git

public class OnMergeSpec extends JTEPipelineSpecification {
	def OnMerge = null

	def setup() {
		OnMerge = loadPipelineScriptForStep('git', 'on_merge')

		// Redirect to System.out for troubleshooting purposes
		OnMerge.getBinding().setProperty('out', System.out)
		//explicitlyMockPipelineVariable('out')

		explicitlyMockPipelineStep('get_feature_branch_sha')
		explicitlyMockPipelineStep('get_merged_from')
	}

	def 'verify do nothing if not merge' () {
		setup:
			def env = [GIT_BUILD_CAUSE: 'pr']
			OnMerge.getBinding().setVariable('env', env)
		when:
			OnMerge([:], { echo 'body' })
		then:
			0 * getPipelineMock('echo')('body')
	}

	def 'do nothing if source branch does not match' () {
		setup:
			def env = [GIT_BUILD_CAUSE: 'merge']
			OnMerge.getBinding().setVariable('env', env)

			getPipelineMock("get_feature_branch_sha")() >> "abcd12345"
			getPipelineMock("get_merged_from")() >> ["feature-two"]
		when:
			OnMerge([from:"feature-one"], { echo 'body' })
		then:
			OnMerge.getBinding().variables.env.FEATURE_SHA == "abcd12345"
			0 * getPipelineMock('echo')('body')
	}

	def 'do nothing if target branch does not match' () {
		setup:
			def env = [GIT_BUILD_CAUSE: 'merge', BRANCH_NAME: 'test']
			OnMerge.getBinding().setVariable('env', env)

			getPipelineMock("get_feature_branch_sha")() >> "abcd12345"
			getPipelineMock("get_merged_from")() >> ["feature-two"]
		when:
			OnMerge([to:"develop"], { echo 'body' })
		then:
			OnMerge.getBinding().variables.env.FEATURE_SHA == "abcd12345"
			0 * getPipelineMock('echo')('body')		
	}

	def 'verify running because of a merge from source to target' () {
		setup:
			def env = [GIT_BUILD_CAUSE: 'merge', BRANCH_NAME: 'develop']
			OnMerge.getBinding().setVariable('env', env)

			getPipelineMock("get_feature_branch_sha")() >> "abcd12345"
			getPipelineMock("get_merged_from")() >> ["feature-one"]
		when:
			OnMerge([from:"feature-one", to:"develop"], { echo 'body' })
		then:
			OnMerge.getBinding().variables.env.FEATURE_SHA == "abcd12345"
			1 * getPipelineMock('echo')('body')		
	}
}

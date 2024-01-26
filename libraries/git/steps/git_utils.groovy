package libraries.git.steps

def get_merged_from(){
  node{
    unstash "workspace"
    // update remote for git name-rev to properly work
    def remote = env.GIT_URL
    def cred_id = env.GIT_CREDENTIAL_ID
    withCredentials([usernamePassword(credentialsId: cred_id, passwordVariable: 'PASS', usernameVariable: 'USER')]){
        remote = remote.replaceFirst("://", "://${USER}:${PASS}@")
        sh "git remote rm origin"
        sh "git remote add origin ${remote}"
        sh "git fetch --all > /dev/null 2>&1"
    }
    // list all shas, but trim the first two shas
    // the first sha is the current commit
    // the second sha is the current commit's parent
    def sourceShas = sh(
      script: "git rev-list HEAD --parents -1",
      returnStdout: true
    ).trim().split(" ")[2..-1]
    def branchNames = []
    // loop through all shas and attempt to turn them into branch names
    for(sha in sourceShas) {
      def branch = sh(
        script: "git name-rev --name-only " + sha,
        returnStdout: true
      ).replaceFirst("remotes/origin/", "").trim()
      // trim the ~<number> off branch names which means commits back
      // e.g. master~4 means 4 commits ago on master
      if(branch.contains("~"))
        branch = branch.substring(0, branch.lastIndexOf("~"))
      if(!branch.contains("^"))
        branchNames.add(branch)
    }
    return branchNames
  }
}

def get_feature_branch_sha(){
  node{
    unstash "workspace"
    sh(
      script: "git rev-parse \$(git --no-pager log -n1 | grep Merge: | awk '{print \$3}')",
      returnStdout: true
     ).trim()
  }
}
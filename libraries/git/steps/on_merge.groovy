/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.git.steps

void call(Map args = [:], body){
  println "on_merge running"
  println "args.to : ${args.to}"
  println "args.from : ${args.from}"

  // do nothing if not merge
  if (!env.GIT_BUILD_CAUSE.equals("merge"))
    return

  env.FEATURE_SHA = get_feature_branch_sha()

  println "env.GIT_BUILD_CAUSE : ${env.GIT_BUILD_CAUSE}"
  println "env.FEATURE_SHA : ${env.FEATURE_SHA}"

  def source_branch = get_merged_from()
  def target_branch = env.BRANCH_NAME

  println "source_branch : ${source_branch}"
  println "target_branch : ${target_branch}"

  // do nothing if source branch doesn't match
  if (args.from)
  if (!source_branch.collect{ it ==~ args.from}.contains(true)){
    println "do nothing, source branch doesn't match"
    return
  }

  // do nothing if target branch doesnt match
  if (args.to)
  if (!(target_branch ==~ args.to)){
    println "do nothing, target branch doesn't match"
    return
  }

  def mergedFrom = source_branch.join(", ")
  // grammar essentially, remove oxford comma to follow git syntax
  if(mergedFrom.contains(", ")) {
      def oxford = mergedFrom.lastIndexOf(", ")
      mergedFrom = mergedFrom.substring(0, oxford) + " and" + mergedFrom.substring(oxford + 1)
  }

  println "running because of a merge from ${mergedFrom} to ${target_branch}"
  body()
}

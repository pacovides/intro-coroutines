package tasks

import contributors.*

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit,
) {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    val users: MutableList<User> = mutableListOf()
    for (repo: Repo in repos) {
        val newUsers = service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }.body() ?: emptyList()
        users.addAll(newUsers)
        updateResults(users.aggregate(), false)
    }

    //Could be avoided with a countdown in the loop above
    updateResults(users.aggregate(), true)
}
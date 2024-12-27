package tasks

import contributors.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit,
) {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    coroutineScope {
        val channel = Channel<List<User>>()
        val users: MutableList<User> = mutableListOf()
        for (repo: Repo in repos) {
            launch {
                val newUsers =
                    service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }.body() ?: emptyList()
                channel.send(newUsers)
            }
        }
        repeat(repos.size) {
            users.addAll(channel.receive())
            updateResults(users.aggregate(), it == repos.lastIndex)
        }
    }
}

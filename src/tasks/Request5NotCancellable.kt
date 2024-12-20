package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    //Idiomatic
    val deferredUsers: List<Deferred<List<User>>> = repos.map { repo ->
        GlobalScope.async(Dispatchers.Default) {
            log("starting loading for ${repo.name}")
            //We can add an artificial delay
            delay(3000)
            service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .body() ?: emptyList()
        }
    }
    return deferredUsers.awaitAll().flatten().aggregate()
}
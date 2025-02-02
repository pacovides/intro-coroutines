package tasks

import contributors.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    // My first attempt
    //    val deferredUsers: MutableList<Deferred<List<User>>> = mutableListOf()
    //    for (repo: Repo in repos) {
    //        deferredUsers.add(async {
    //            service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }.body() ?: emptyList()
    //        })
    //    }

    //Idiomatic
    val deferredUsers: List<Deferred<List<User>>> = repos.map { repo ->
        async {
            log("starting loading for ${repo.name}")
            service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .body() ?: emptyList()
        }
    }
    deferredUsers.awaitAll().flatten().aggregate()
}
package tasks

import contributors.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CountDownLatch

fun loadContributorsCallbacks(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    service.getOrgReposCall(req.org).onResponse { responseRepos ->
        logRepos(req, responseRepos)
        val repos = responseRepos.bodyList()
        val allUsers = mutableListOf<User>()
        val countDownLatch = CountDownLatch(repos.size)
        for (repo in repos) {
            service.getRepoContributorsCall(req.org, repo.name).onResponse { responseUsers ->
                logUsers(repo, responseUsers)
                val users = responseUsers.bodyList()
                allUsers += users

                // A naive attempt to call updateResults here kind of works, but may result in
                // concurrentModificationException. It is also inefficient.
                //updateResults(allUsers.aggregate())

                //Instead we could check and do it only for the last index (assume we pass i as counter)
                // ... but there is no guarantee last repo ends last, this is no good.
                //                if(i == repos.lastIndex){
                //                    updateResults(allUsers.aggregate())
                //                }
                //We could also count instead and increment an AtomicInteger here and be done when we reach n
                //But better yer, we use the countdown latch.
                countDownLatch.countDown()
            }
        }
        // This code doesn't work. How to fix that? (we are calling before users are loaded)
        //updateResults(allUsers.aggregate())

        countDownLatch.await()
        updateResults(allUsers.aggregate())
    }
}

inline fun <T> Call<T>.onResponse(crossinline callback: (Response<T>) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            callback(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            log.error("Call failed", t)
        }
    })
}

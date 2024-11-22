package tasks

import contributors.User

/*

 In the initial list each user is present several times, once for each
 repository he or she contributed to.
 Merge duplications: each user should be present only once in the resulting list
 with the total value of contributions for all the repositories.
 Users should be sorted in a descending order by their contributions.

 The corresponding test can be found in test/tasks/AggregationKtTest.kt.
 You can use 'Navigate | Test' menu action (note the shortcut) to navigate to the test.

 Textbook solution:
 fun List<User>.aggregate(): List<User> =
    groupBy { it.login }
        .map { (login, group) -> User(login, group.sumOf { it.contributions }) }
        .sortedByDescending { it.contributions }


*/
fun List<User>.aggregate(): List<User> {
    val contributionMap: MutableMap<String, Int> = this.associate { it.login to 0 }.toMutableMap()
    this.forEach {
        contributionMap[it.login] = (contributionMap[it.login]?:0) + it.contributions
    }
    return contributionMap.map { User(it.key, it.value) }.sortedByDescending { it.contributions }
}
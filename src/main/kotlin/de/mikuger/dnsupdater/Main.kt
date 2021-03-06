package de.mikuger.dnsupdater

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder
import com.amazonaws.services.route53.model.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.jackson.responseObject

fun main(args: Array<String>) {
    val ip = Fuel.get("https://api.ipify.org?format=json")
        .responseObject<IPResult>()
        .third.get()
        .ip
    println("IP: $ip")
    val envCred = EnvironmentVariableCredentialsProvider()
    val amzClient = AmazonRoute53ClientBuilder.standard()
        .apply {
            credentials = envCred
            region = Regions.EU_WEST_1.name
        }
        .build()

    val recordsEnv = System.getenv("DNS_RECORDS_TO_UPDATE")
    println("DNS records to check: $recordsEnv")
    val recordsToUpdate = recordsEnv
        .split(",")
        .filter { it.isNotBlank() }
        .map { it.trim() }
        .map{ it.replace("*", "\\052")}
        .toList()

    val hostedZone = System.getenv("DNS_HOSTED_ZONES")
    val resourceRecordSets = amzClient.listResourceRecordSets(ListResourceRecordSetsRequest(hostedZone))
        .resourceRecordSets
    resourceRecordSets.forEach {
        println(it)
    }
    val changes = resourceRecordSets
        .filter { it.type == "A" }
        .filter { it.name in recordsToUpdate }
        .filter { it.resourceRecords.size == 1 }
        .filter { it.resourceRecords[0].value != ip }
        .map {
            it.resourceRecords[0].value = ip
            Change(ChangeAction.UPSERT, it)
        }
        .toList()

    if (changes.isNotEmpty()) {
        println("Updating ${changes.size} records:")
        changes
            .map {
                it.resourceRecordSet.run {
                    " ${name} -> ${resourceRecords[0].value}"
                }
            }
            .forEach { println("\t$it") }
        amzClient.changeResourceRecordSets(
            ChangeResourceRecordSetsRequest(
                hostedZone,
                ChangeBatch(changes)
            )
        )
        println("${changes.size} records successfully updated")
    } else {
        println("Nothing to update.")
    }

    val deadManSwitchUrl = System.getenv("DNS_DEAD_MAN_SWITCH_URL").trim()
    println("Dead Man's Switch URL: $deadManSwitchUrl")
    Fuel.get(deadManSwitchUrl)
        .responseString()
}

object Main {}

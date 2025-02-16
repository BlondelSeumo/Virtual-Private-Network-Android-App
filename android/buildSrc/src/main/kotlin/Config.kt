import java.util.Date

/**
 * Created by Furuichi on 15/09/2023
 */
val autoGenVersionCode: Int
    get() = (Date().time / 60000).toInt()

object Config {
    const val applicationId: String = "app.witwork.vpn"
    const val versionName: String = "1.0.17"
    const val admobId = "########-####-####-####-############"
    const val oneSignalKey = "########-####-####-####-############"

    object Version {
        const val minSdk = 26
        const val targetSdk = 34
        const val compileSdk = 34
    }

}

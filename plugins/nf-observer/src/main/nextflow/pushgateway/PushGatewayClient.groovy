package nextflow.pushgateway

import java.net.URL

import groovy.util.logging.Slf4j
import org.apache.http.HttpResponse



@Slf4j
class PushgatewayClient {
    HttpResponse postData(String metricName, String metricValue) {
        def post = new URL("https://prom-aggregation-gateway.lims.dev.locusdev.net/metrics/job/some_job/instance/some_instance").openConnection()
        def message = 
        """
        # TYPE nextflow gauge
        $metricName $metricValue
        """

        log.info "Posting data to pushgateway: '$message'"

        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.getOutputStream().write(message.bytes)

        assert post.responseCode == 200

        log.info "Response code: '${post.responseCode}'"
    }
}

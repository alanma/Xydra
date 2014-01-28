package org.xydra.xgae.gaeutils;

import org.xydra.index.query.Pair;

import com.google.appengine.api.quota.QuotaService;
import com.google.appengine.api.quota.QuotaService.DataType;
import com.google.appengine.api.quota.QuotaServiceFactory;


public class QuotaUtils {
	
	/**
	 * @return apiTime and cpuTime of the current running request (or -1 for
	 *         undefined values).
	 */
	@SuppressWarnings("deprecation")
	public static Pair<Long,Long> getCurrentQuotas() {
		QuotaService qs = QuotaServiceFactory.getQuotaService();
		long apiTime = -1;
		if(qs.supports(DataType.API_TIME_IN_MEGACYCLES)) {
			apiTime = qs.getApiTimeInMegaCycles();
		}
		long cpuTime = -1;
		if(qs.supports(DataType.CPU_TIME_IN_MEGACYCLES)) {
			cpuTime = qs.getCpuTimeInMegaCycles();
		}
		return new Pair<Long,Long>(apiTime, cpuTime);
	}
	
}

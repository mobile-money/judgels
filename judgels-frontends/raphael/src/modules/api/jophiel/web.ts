import { APP_CONFIG } from 'conf';
import { get } from 'modules/api/http';

export interface WebConfig {
  recaptcha?: {
    siteKey: string;
  };
  userRegistration: {
    useRecaptcha: boolean;
  };
}

export function createWebAPI() {
  const baseURL = `${APP_CONFIG.apiUrls.jophiel}/web`;

  return {
    getWebConfig: (): Promise<WebConfig> => {
      return get(`${baseURL}/config`);
    },
  };
}

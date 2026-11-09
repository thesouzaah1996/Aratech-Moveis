(() => {
  async function token() {
    const r = await fetch('/aratech/auth/csrf', {credentials:'same-origin',cache:'no-store'});
    if(!r.ok) throw new Error('Não foi possível renovar a proteção CSRF.');
    return (await r.json()).token;
  }
  window.ui=SwaggerUIBundle({url:'/v3/api-docs',dom_id:'#swagger-ui',deepLinking:true,filter:true,docExpansion:'none',tagsSorter:'alpha',operationsSorter:'alpha',persistAuthorization:false,withCredentials:true,validatorUrl:null,displayRequestDuration:true,defaultModelsExpandDepth:0,presets:[SwaggerUIBundle.presets.apis,SwaggerUIStandalonePreset],layout:'BaseLayout',
    requestInterceptor:async req=>{
      const destino=new URL(req.url,location.origin);
      if(destino.origin!==location.origin)throw new Error('A documentação executa requisições somente neste servidor.');
      if(!['GET','HEAD','OPTIONS'].includes((req.method||'GET').toUpperCase())){req.headers=req.headers||{};req.headers['X-XSRF-TOKEN']=await token();}
      return req;
    }
  });
})();

ARG NODE_IMAGE=crpi-4wiolv963xmb1wvm.cn-hangzhou.personal.cr.aliyuncs.com/hangzhoulingjian/node:20.18.0-alpine
ARG NGINX_IMAGE=crpi-4wiolv963xmb1wvm.cn-hangzhou.personal.cr.aliyuncs.com/hangzhoulingjian/nginx:1.28.3-alpine
ARG FOREST_ENV=local
ARG TRADE_LEADS_BACKEND_NODE_1=trade-leads-backend:8081
ARG TRADE_LEADS_BACKEND_NODE_2=

# Add one frontend build stage per app that should be served by the shared gateway.
FROM ${NODE_IMAGE} AS build-trade-leads-platform-web

WORKDIR /workspace

RUN npm install -g pnpm@10.33.0

COPY base-frontend/package.json base-frontend/package.json
COPY base-frontend/pnpm-lock.yaml base-frontend/pnpm-lock.yaml
COPY base-frontend/pnpm-workspace.yaml base-frontend/pnpm-workspace.yaml
COPY base-frontend/turbo.json base-frontend/turbo.json
COPY base-frontend/tsconfig.base.json base-frontend/tsconfig.base.json

COPY base-frontend/packages/http-client/package.json base-frontend/packages/http-client/package.json
COPY base-frontend/packages/ui-kit/package.json base-frontend/packages/ui-kit/package.json
COPY business/domains/file/frontend/package.json business/domains/file/frontend/package.json
COPY business/domains/verification/frontend/package.json business/domains/verification/frontend/package.json
COPY business/domains/user/frontend/package.json business/domains/user/frontend/package.json
COPY business/domains/lead/frontend/package.json business/domains/lead/frontend/package.json
COPY business/domains/point/frontend/package.json business/domains/point/frontend/package.json
COPY business/aggregations/user-point/frontend/package.json business/aggregations/user-point/frontend/package.json
COPY business/aggregations/user-lead/frontend/package.json business/aggregations/user-lead/frontend/package.json
COPY apps/trade-leads/clients/platform-web/package.json apps/trade-leads/clients/platform-web/package.json

RUN pnpm --dir base-frontend install --frozen-lockfile

COPY base-frontend/packages base-frontend/packages
COPY business/domains/file/frontend business/domains/file/frontend
COPY business/domains/verification/frontend business/domains/verification/frontend
COPY business/domains/user/frontend business/domains/user/frontend
COPY business/domains/lead/frontend business/domains/lead/frontend
COPY business/domains/point/frontend business/domains/point/frontend
COPY business/aggregations/user-point/frontend business/aggregations/user-point/frontend
COPY business/aggregations/user-lead/frontend business/aggregations/user-lead/frontend
COPY apps/trade-leads/clients/platform-web apps/trade-leads/clients/platform-web

RUN pnpm --dir base-frontend --filter @forest/trade-leads-platform-web build

FROM ${NGINX_IMAGE}
ARG FOREST_ENV
ARG TRADE_LEADS_BACKEND_NODE_1
ARG TRADE_LEADS_BACKEND_NODE_2

RUN mkdir -p /etc/nginx/ssl /etc/nginx/snippets/sites /usr/share/nginx/html/trade-leads/platform-web \
    && rm -f /etc/nginx/conf.d/default.conf

COPY apps/gateway/nginx/nginx.conf /etc/nginx/nginx.conf
COPY apps/gateway/nginx/conf.d /tmp/nginx-conf.d
COPY apps/gateway/nginx/snippets /etc/nginx/snippets
RUN case "$FOREST_ENV" in \
        local) cp -R /tmp/nginx-conf.d/local/. /etc/nginx/conf.d ;; \
        prod) cp -R /tmp/nginx-conf.d/prod/. /etc/nginx/conf.d \
            && sed -i "s|__TRADE_LEADS_BACKEND_NODE_1__|${TRADE_LEADS_BACKEND_NODE_1}|g" /etc/nginx/conf.d/trade-leads.conf \
            && if [ -n "${TRADE_LEADS_BACKEND_NODE_2}" ]; then \
                sed -i "s|__TRADE_LEADS_BACKEND_NODE_2_LINE__|    server ${TRADE_LEADS_BACKEND_NODE_2} max_fails=3 fail_timeout=10s;|g" /etc/nginx/conf.d/trade-leads.conf; \
            else \
                sed -i '/__TRADE_LEADS_BACKEND_NODE_2_LINE__/d' /etc/nginx/conf.d/trade-leads.conf; \
            fi ;; \
        *) echo "Unsupported FOREST_ENV: $FOREST_ENV" >&2; exit 1 ;; \
    esac \
    && rm -rf /tmp/nginx-conf.d
# Add one dist COPY per connected app, using /usr/share/nginx/html/<app>/<site>.
COPY --from=build-trade-leads-platform-web /workspace/apps/trade-leads/clients/platform-web/dist /usr/share/nginx/html/trade-leads/platform-web

EXPOSE 80
EXPOSE 443

/*
 * Copyright (c) 2020 Dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.panda_lang.reposilite.repository;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.panda_lang.reposilite.Reposilite;
import org.panda_lang.reposilite.ReposiliteContext;
import org.panda_lang.reposilite.ReposiliteContextFactory;
import org.panda_lang.reposilite.error.ResponseUtils;

public final class DeployController implements Handler {

    private final ReposiliteContextFactory contextFactory;
    private final DeployService deployService;

    public DeployController(ReposiliteContextFactory contextFactory, DeployService deployService) {
        this.contextFactory = contextFactory;
        this.deployService = deployService;
    }

    @Override
    public void handle(Context ctx) {
        ReposiliteContext context = contextFactory.create(ctx);
        Reposilite.getLogger().info("DEPLOY " + context.uri() + " from " + context.address());

        deployService.deploy(context)
                .map(future -> ctx.result(future.thenAccept(result -> result
                        .map(ctx::json)
                        .mapError(error -> ResponseUtils.errorResponse(ctx, error)))))
                .onError(error -> ResponseUtils.errorResponse(ctx, error));
    }

}

/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.service.mapper.hateoas;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class HateoasContextHolder {
    private static HateoasContextHolder instance;

    private final List<Service> hateoasServices;

    public HateoasContextHolder() {
        this.hateoasServices = new ArrayList<>();
    }

    public static HateoasContextHolder getHateoasContextHolder() {
        synchronized (HateoasContextHolder.class) {
            if (Objects.isNull(instance)) {
                instance = new HateoasContextHolder();
            }
        }
        return instance;
    }

    public void updateHateoasResource(String packageId, int serviceId, String resourceName, Resource resource) {
        Optional<Service> hateoasService = this.hateoasServices.stream()
                .filter(svc -> packageId.equals(svc.getPackageId()) && svc.getServiceId() == serviceId)
                .findFirst();
        if (hateoasService.isEmpty()) {
            Service service = new Service(packageId, serviceId);
            service.addResource(resourceName, resource);
            this.hateoasServices.add(service);
            return;
        }
        Service service = hateoasService.get();
        service.addResource(resourceName, resource);
    }

    public Optional<Resource> getHateoasResource(String packageId, int serviceId, String resourceName,
                                                 String resourceMethod) {
        return this.hateoasServices.stream()
                .filter(svc -> svc.getPackageId().equals(packageId) && svc.getServiceId() == serviceId)
                .findFirst()
                .flatMap(svc -> svc.getHateoasResourceMapping().entrySet().stream()
                        .filter(resources -> resourceName.equals(resources.getKey()))
                        .findFirst()
                ).flatMap(hateoasResourceMapping -> hateoasResourceMapping.getValue().stream()
                        .filter(resource -> resourceMethod.equals(resource.resourceMethod()))
                        .findFirst()
                );
    }
}

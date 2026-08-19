package org.ficha.application.usecase;

@FunctionalInterface
public interface UseCase<RequestType, ResponseType> {
    ResponseType execute(RequestType request);
}

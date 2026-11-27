package com.submate.backend.subscription.support;

public class DomainException extends RuntimeException {
    private final int status;
    public DomainException(int status, String message) { super(message); this.status = status; }
    public int getStatus() { return status; }
    public static DomainException notFound(String resource) { return new DomainException(404, resource + " 정보를 찾을 수 없습니다."); }
    public static DomainException conflict(String message) { return new DomainException(409, message); }
    public static DomainException unauthorized(String message) { return new DomainException(401, message); }
    public static DomainException forbidden() { return new DomainException(403, "접근 권한이 없습니다."); }
}

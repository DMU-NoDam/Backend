# Class 책임
- Controller, Service, Transaction 실행 구간에 AOP 로그를 적용한다.
- 각 구간의 시작, 성공, 실패, 실행 함수명, 소요 시간을 기록한다.
# 함수
- public Object logController(ProceedingJoinPoint joinPoint) throws Throwable
- - `@RestController`, `@Controller` 대상 요청 처리 로그를 기록한다.
- public Object logService(ProceedingJoinPoint joinPoint) throws Throwable
- - `@Service` 대상 비즈니스 로직 로그를 기록한다.
- public Object logTransaction(ProceedingJoinPoint joinPoint) throws Throwable
- - `@Transactional` 및 `TransactionTemplate.execute*` 실행 로그를 기록한다.
# todo
- 없음

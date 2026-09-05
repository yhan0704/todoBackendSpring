-- 연습용 예시 데이터.
-- 앱을 시작할 때마다(spring.sql.init.mode=always) 실행되지만,
-- users는 email UNIQUE 제약을 이용한 ON CONFLICT로, todos는 NOT EXISTS 체크로
-- 중복 삽입을 막아서 재시작해도 데이터가 계속 늘어나지 않게 했음.

-- 비밀번호는 전부 BCrypt로 미리 인코딩한 "password123" 해시 (로그인 테스트용).
INSERT INTO users (name, email, password, refresh_token, nickname, phone, birth_date)
VALUES
    ('김민준', 'minjun@example.com', '$2a$10$1aLPpmQjFp1Jw1tLLkCB5OXmgPKrUreV5kj1qmPe2PaF6PP0Ii.Gq', NULL, '민쥰', '010-1111-2222', '1996-03-15'),
    ('이서연', 'seoyeon@example.com', '$2a$10$1aLPpmQjFp1Jw1tLLkCB5OXmgPKrUreV5kj1qmPe2PaF6PP0Ii.Gq', NULL, '서니', '010-2222-3333', '1998-07-22'),
    ('박도윤', 'doyoon@example.com',  '$2a$10$1aLPpmQjFp1Jw1tLLkCB5OXmgPKrUreV5kj1qmPe2PaF6PP0Ii.Gq', NULL, '도니', '010-3333-4444', '1994-11-02'),
    ('최지우', 'jiwoo@example.com',   '$2a$10$1aLPpmQjFp1Jw1tLLkCB5OXmgPKrUreV5kj1qmPe2PaF6PP0Ii.Gq', NULL, '지우', '010-4444-5555', '2000-01-30'),
    ('정하은', 'haeun@example.com',   '$2a$10$1aLPpmQjFp1Jw1tLLkCB5OXmgPKrUreV5kj1qmPe2PaF6PP0Ii.Gq', NULL, NULL, NULL, NULL)
ON CONFLICT (email) DO NOTHING;

-- user_id는 email로 서브쿼리해서 채움 — INSERT 순서상 users가 먼저 들어가 있어야 하고,
-- IDENTITY라 id를 미리 알 수 없기 때문. 일부러 유저마다 todo 개수를 다르게(3/2/2/2/0),
-- 그리고 하나는 주인 없는(user_id NULL) todo로 남겨서 INNER JOIN과 LEFT JOIN의
-- 결과 차이가 실제로 보이게 함. 정하은(haeun)은 todo가 0개라 LEFT JOIN에서만 나타남.

-- 김민준: 3개
INSERT INTO todos (tasks, done, priority, due_date, category, user_id, created_at, updated_at)
SELECT '장보기', false, 'HIGH', '2026-09-06', '집안일', (SELECT id FROM users WHERE email = 'minjun@example.com'), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE tasks = '장보기');

INSERT INTO todos (tasks, done, priority, due_date, category, user_id, created_at, updated_at)
SELECT 'JPA 연관관계 공부하기', false, 'HIGH', '2026-09-10', '공부', (SELECT id FROM users WHERE email = 'minjun@example.com'), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE tasks = 'JPA 연관관계 공부하기');

INSERT INTO todos (tasks, done, priority, due_date, category, user_id, created_at, updated_at)
SELECT '스프링 시큐리티 정리', false, 'HIGH', '2026-09-08', '공부', (SELECT id FROM users WHERE email = 'minjun@example.com'), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE tasks = '스프링 시큐리티 정리');

-- 이서연: 2개
INSERT INTO todos (tasks, done, priority, due_date, category, user_id, created_at, updated_at)
SELECT '헬스장 가기', true, 'MEDIUM', '2026-09-04', '운동', (SELECT id FROM users WHERE email = 'seoyeon@example.com'), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE tasks = '헬스장 가기');

INSERT INTO todos (tasks, done, priority, due_date, category, user_id, created_at, updated_at)
SELECT '이력서 수정', false, 'MEDIUM', '2026-09-20', '커리어', (SELECT id FROM users WHERE email = 'seoyeon@example.com'), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE tasks = '이력서 수정');

-- 박도윤: 2개
INSERT INTO todos (tasks, done, priority, due_date, category, user_id, created_at, updated_at)
SELECT '치과 예약', false, 'LOW', '2026-09-15', '건강', (SELECT id FROM users WHERE email = 'doyoon@example.com'), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE tasks = '치과 예약');

INSERT INTO todos (tasks, done, priority, due_date, category, user_id, created_at, updated_at)
SELECT '코드 리뷰 반영하기', false, 'HIGH', '2026-09-05', '업무', (SELECT id FROM users WHERE email = 'doyoon@example.com'), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE tasks = '코드 리뷰 반영하기');

-- 최지우: 2개
INSERT INTO todos (tasks, done, priority, due_date, category, user_id, created_at, updated_at)
SELECT '방 청소', true, 'LOW', '2026-09-01', '집안일', (SELECT id FROM users WHERE email = 'jiwoo@example.com'), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE tasks = '방 청소');

INSERT INTO todos (tasks, done, priority, due_date, category, user_id, created_at, updated_at)
SELECT '친구 생일 선물 사기', false, 'MEDIUM', '2026-09-12', '개인', (SELECT id FROM users WHERE email = 'jiwoo@example.com'), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE tasks = '친구 생일 선물 사기');

-- 주인 없는 todo (user_id NULL) — LEFT JOIN 연습용
INSERT INTO todos (tasks, done, priority, due_date, category, user_id, created_at, updated_at)
SELECT '독서 30분', false, 'LOW', NULL, '자기계발', NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE tasks = '독서 30분');

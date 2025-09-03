-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;
DROP TABLE IF EXISTS exam_question_selected_answers;
CREATE TABLE exam_question_selected_answers (
                                                exam_question_id BIGINT NOT NULL,
                                                answer_id BIGINT NOT NULL,
                                                PRIMARY KEY (exam_question_id, answer_id),
                                                FOREIGN KEY (exam_question_id) REFERENCES exam_question(id) ON DELETE CASCADE,
                                                FOREIGN KEY (answer_id) REFERENCES answer(id) ON DELETE CASCADE
);
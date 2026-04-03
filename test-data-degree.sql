-- 学位管理模块测试数据

-- 1. 论文进展数据（包括开题、中期、预答辩）
INSERT INTO thesis_progress (
    student_id, student_no, student_name, mentor_id, mentor_name, thesis_title,
    progress_type, content, mentor_status, mentor_comment, mentor_time,
    secretary_status, secretary_comment, secretary_time, dean_status, dean_comment, dean_time,
    overall_status, submit_time, event_time, location, attachment_path, committee_members,
    create_time, update_time
) VALUES
-- 学生 2 的论文进展数据
(2, '22040000', '张三', 2, '李四', '基于深度学习的图像识别研究',
1, '{"background":"深度学习在图像识别领域应用广泛","research_status":"目前研究处于起步阶段","research_content":"主要研究卷积神经网络在图像分类中的应用","research_method":"采用实验分析法","expected_results":"预期达到95%的准确率","research_plan":"分为三个阶段完成","references":"[1] 深度学习入门, [2] 计算机视觉基础","proposal_time":"2024-03-15 10:00:00","proposal_location":"会议室A","committee_members":"李四,苑成军,刘琳"}',
1, '研究内容明确，方法可行', '2024-03-16 09:30:00',
1, '材料齐全，符合要求', '2024-03-17 14:20:00',
1, '通过', '2024-03-18 16:00:00',
4, '2024-03-15 10:00:00', '2024-03-15 10:00:00', '会议室A', '/upload/proposal/22040000.doc', '李四,苑成军,刘琳',
NOW(), NOW()),
(2, '22040000', '张三', 2, '李四', '基于深度学习的图像识别研究',
2, '{"progress_report":"已完成文献综述和数据收集","completed_work":"已完成CNN模型的初步搭建","remaining_work":"还需优化模型参数","problems":"数据量不足","next_plan":"增加数据集，优化模型","draft_progress":60}',
1, '进展顺利，已完成阶段性目标', '2024-05-20 10:15:00',
1, '中期检查合格', '2024-05-21 11:30:00',
0, null, null,
3, '2024-05-20 10:00:00', '2024-05-20 10:00:00', '会议室B', '/upload/midterm/22040000.doc', '李四,姜德迅,丛二勇',
NOW(), NOW()),

-- 学生 4 的论文进展数据
(4, '22023237', '詹宇昊', 4, '苑成军', '自然语言处理在情感分析中的应用',
1, '{"background":"情感分析在社交媒体分析中具有重要价值","research_status":"已有相关研究","research_content":"研究基于LSTM的情感分类算法","research_method":"文本挖掘和机器学习方法","expected_results":"预期准确率达到90%","research_plan":"分为四个阶段","references":"[1] 自然语言处理基础, [2] 深度学习在NLP中的应用","proposal_time":"2024-03-20 09:30:00","proposal_location":"会议室C","committee_members":"苑成军,徐雪,李1"}',
1, '研究方向具有实际应用价值', '2024-03-21 09:00:00',
0, null, null,
0, null, null,
2, '2024-03-20 09:30:00', '2024-03-20 09:30:00', '会议室C', '/upload/proposal/22023237.doc', '苑成军,徐雪,李1',
NOW(), NOW()),
(4, '22023237', '詹宇昊', 4, '苑成军', '自然语言处理在情感分析中的应用',
3, '{"abstract_content":"本文研究了基于双向LSTM的情感分析方法，通过对社交媒体文本的分析，实现了情感分类功能"}',
0, null, null,
0, null, null,
0, null,
0, '2024-06-10 10:00:00', '2024-06-10 10:00:00', '会议室D', '/upload/predefense/22023237.pdf', '苑成军,徐雪,李1',
NOW(), NOW()),

-- 学生 7 的论文进展数据
(7, '22042204', '李晓晓', 5, '徐雪', '大数据分析在用户行为预测中的应用',
1, '{"background":"用户行为预测对商业决策至关重要","research_status":"相关研究已较为成熟","research_content":"研究基于随机森林的用户行为预测模型","research_method":"数据分析和机器学习","expected_results":"预期准确率达到85%","research_plan":"分为三个阶段","references":"[1] 机器学习实战, [2] 大数据分析","proposal_time":"2024-03-18 14:00:00","proposal_location":"会议室B","committee_members":"徐雪,刘琳,姜德迅"}',
1, '研究方法成熟可行', '2024-03-19 10:30:00',
1, '材料齐全，结构清晰', '2024-03-20 15:40:00',
1, '通过', '2024-03-21 16:20:00',
4, '2024-03-18 14:00:00', '2024-03-18 14:00:00', '会议室B', '/upload/proposal/22042204.doc', '徐雪,刘琳,姜德迅',
NOW(), NOW()),
(7, '22042204', '李晓晓', 5, '徐雪', '大数据分析在用户行为预测中的应用',
2, '{"progress_report":"已完成数据收集和预处理","completed_work":"已完成模型的初步训练","remaining_work":"还需优化模型参数","problems":"计算资源不足","next_plan":"优化算法，提高计算效率","draft_progress":65}',
1, '已完成阶段性工作', '2024-05-22 09:30:00',
1, '中期检查合格', '2024-05-23 14:15:00',
1, '通过', '2024-05-24 15:30:00',
4, '2024-05-22 09:30:00', '2024-05-22 09:30:00', '会议室C', '/upload/midterm/22042204.doc', '徐雪,苑成军,于少鹏',
NOW(), NOW()),
(7, '22042204', '李晓晓', 5, '徐雪', '大数据分析在用户行为预测中的应用',
3, '{"abstract_content":"本文研究了基于集成学习的用户行为预测方法，通过对用户历史数据的分析，实现了用户购买行为的预测"}',
1, '论文内容完整，结论明确', '2024-06-10 10:00:00',
1, '预答辩合格', '2024-06-11 14:30:00',
1, '通过', '2024-06-12 15:45:00',
4, '2024-06-10 10:00:00', '2024-06-10 10:00:00', '会议室A', '/upload/predefense/22042204.pdf', '徐雪,李1,王克朝',
NOW(), NOW());

-- 2. 论文答辩数据
INSERT INTO thesis_defense (
    student_id, student_no, student_name, mentor_id, mentor_name, thesis_title,
    thesis_final_url, tutor_approval, tutor_id, tutor_approval_time, dean_approval, dean_approval_time,
    defense_date, defense_location, committee_chair, committee_members, defense_result, defense_score, defense_comments, qa_record, status, create_time, update_time
) VALUES
-- 学生 7 的答辩数据（已完成）
(7, '22042204', '李晓晓', 5, '徐雪', '大数据分析在用户行为预测中的应用',
'/upload/thesis/22042204_final.pdf', 1, 5, '2024-06-15 10:00:00', 1, '2024-06-16 14:30:00',
'2024-06-20 09:30:00', '图书馆学术报告厅', '徐雪', '苑成军,姜德迅,于少鹏', 1, 88.5,
'论文研究内容新颖，方法科学，结论明确，达到硕士学位论文要求',
'Q: 请解释你的研究方法？\nA: 我采用集成学习方法，将多个分类器的结果进行融合...', 2, NOW(), NOW()),

-- 学生 2 的答辩数据（待答辩）
(2, '22040000', '张三', 2, '李四', '基于深度学习的图像识别研究',
'/upload/thesis/22040000_final.pdf', 1, 2, '2024-06-18 10:30:00', 1, '2024-06-19 15:20:00',
'2024-06-25 09:00:00', '图书馆学术报告厅', '李四', '苑成军,刘琳,丛二勇', 0, null, null, null, 1, NOW(), NOW());

-- 3. 论文外审数据
INSERT INTO thesis_external_review (
    student_id, student_no, student_name, thesis_url, reviewers, review_time, review_result, review_comments, status, create_time, update_time
) VALUES
-- 学生 7 的外审数据（已完成）
(7, '22042204', '李晓晓', '/upload/thesis/22042204_final.pdf', '赵教授(某研究院)', '2024-06-25 14:30:00', 1,
'论文研究具有一定的创新性，方法科学，数据充分，结论合理，达到硕士学位论文水平', 2, NOW(), NOW()),

-- 学生 2 的外审数据（外审中）
(2, '22040000', '张三', '/upload/thesis/22040000_final.pdf', '赵教授(某研究院)', null, 0, null, 1, NOW(), NOW());

-- 4. 学位申请数据
INSERT INTO degree_application (
    student_id, student_no, student_name, mentor_id, mentor_name, degree_type, thesis_title, thesis_attachment,
    defense_time, defense_location, committee_chair, committee_members, defense_result, defense_score, defense_committee_comment, qa_record,
    committee_status, committee_comment, degree_granted, degree_grant_date, certificate_no, attachment_path, submit_time, create_time, update_time
) VALUES
-- 学生 7 的学位申请（已完成，已授予学位）
(7, '22042204', '李晓晓', 5, '徐雪', 1, '大数据分析在用户行为预测中的应用', '/upload/thesis/22042204_final.pdf',
'2024-06-20 09:30:00', '图书馆学术报告厅', '徐雪', '苑成军,姜德迅,于少鹏', 1, 88.5,
'论文研究内容新颖，方法科学，结论明确，达到硕士学位论文要求',
'Q: 请解释你的研究方法？\nA: 我采用集成学习方法，将多个分类器的结果进行融合...',
1, '论文符合硕士学位要求，同意授予学位', 1, '2024-07-01', 'MA2024001', '/upload/degree/22042204.zip', '2024-06-25 16:00:00',
NOW(), NOW()),

-- 学生 2 的学位申请（待审批）
(2, '22040000', '张三', 2, '李四', 1, '基于深度学习的图像识别研究', '/upload/thesis/22040000_final.pdf',
'2024-06-25 09:00:00', '图书馆学术报告厅', '李四', '苑成军,刘琳,丛二勇', 0, null, null, null,
0, null, 0, null, null, '/upload/degree/22040000.zip', '2024-06-30 10:00:00',
NOW(), NOW());


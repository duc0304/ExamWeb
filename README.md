# SpringBoot-Examination-and-Exam-Managing-App
An university project

Features:

Teacher:
- Manage classrooms
  + Add/Remove student in each class
  + Export student_list.xls
  + Add/Edit/Remove assignments
- Create exam (can use to create assignment)
  + Upload .docx file to create own exam
  + Create exam using questions in DB (based on subject, grade, chapter)
  + Auto shuffle questions, choices
- View exam
  + Search for desired exam (subject, grade)
  + View each questions in the exam
  + Export exams.zip (include all .pdf of exams having same id. e.g: exam_0.pdf, exam_1.pdf,...)

Student:
- Join classrooms (Or added by teacher)
- Do assignments, view results
- Do training exams (no class needed), view submissions...

Admin:
- Manage users
- Create open training exam
- Add questions to the question-bank.

All users (except admin):
- Register / Login
- Edit personal info (name, email, change avatar, ...)

ver 1.0 _ 1/13/2024
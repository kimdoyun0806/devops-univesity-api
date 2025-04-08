package com.beyond.university.subject.model.service;

import com.beyond.university.subject.model.mapper.SubjectMapper;
import com.beyond.university.subject.model.vo.Subject;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {
    private final SubjectMapper subjectMapper;

    @Override
    public int getTotalCount(String deptNo) {

        return subjectMapper.selectSubjectCount(deptNo);
    }

    @Override
    public List<Subject> getSubjectsByDeptNo(String deptNo, int page, int numOfRows) {
        int limit = numOfRows;
        int offset = (page - 1) * limit;
        RowBounds rowBounds = new RowBounds(offset, limit);

        return subjectMapper.selectAllByDeptNo(deptNo, rowBounds);
    }
}

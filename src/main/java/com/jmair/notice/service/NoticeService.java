package com.jmair.notice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jmair.auth.dto.UserGrade;
import com.jmair.auth.entity.User;
import com.jmair.notice.dto.NoticeDTO;
import com.jmair.notice.entity.Notice;
import com.jmair.notice.repository.NotiveRepository;

@Service
public class NoticeService {

	private final NotiveRepository	noticeRepository;

	@Autowired
	public NoticeService(NotiveRepository noticeRepository) {
		this.noticeRepository = noticeRepository;
	}

	// 등록
	@Transactional
	public NoticeDTO createNotice(NoticeDTO noticeDTO) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
			throw new IllegalArgumentException("로그인한 사용자 정보가 없습니다.");
		}

		// 관리자 권한 확인
		if (!(currentUser.getUserGrade() == UserGrade.ADMIN || currentUser.getUserGrade() == UserGrade.SUPERADMIN)) {
			throw new IllegalArgumentException("공지사항 등록 권한이 없습니다.");
		}

		Notice notice = new Notice();
		notice.setNoticeTitle(noticeDTO.getTitle());
		notice.setNoticeContent(noticeDTO.getContent());
		notice.setNoticeWriter(currentUser.getUserName());
		notice.setNoticePostTime(LocalDateTime.now());
		notice.setStatus(true);
		// notice.setViews(0);

		Notice saved = noticeRepository.save(notice);

		NoticeDTO result = new NoticeDTO();
		result.setId(saved.getNoticeId());
		result.setTitle(saved.getNoticeTitle());
		result.setContent(saved.getNoticeContent());
		result.setWriter(saved.getNoticeWriter());
		result.setPostTime(saved.getNoticePostTime());
		// result.setViews(saved.getViews());
		result.setStatus(saved.isStatus());
		result.setDeleteTime(saved.getNoticeDeleteTime());
		return result;
	}

	// 전체 조회
	public List<NoticeDTO> getAllNotices() {
		List<Notice> notices = noticeRepository.findByStatusTrueOrderByNoticePostTimeDesc();
		return notices.stream().map(notice -> {
			NoticeDTO dto = new NoticeDTO();
			dto.setId(notice.getNoticeId());
			dto.setTitle(notice.getNoticeTitle());
			dto.setContent(notice.getNoticeContent());
			dto.setWriter(notice.getNoticeWriter());
			dto.setPostTime(notice.getNoticePostTime());
			// dto.setViews(notice.getViews());
			return dto;
		}).collect(Collectors.toList());
	}

	// 상세조회
	public NoticeDTO getDetailNotice(Integer noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
		NoticeDTO dto = new NoticeDTO();
		dto.setId(notice.getNoticeId());
		dto.setTitle(notice.getNoticeTitle());
		dto.setContent(notice.getNoticeContent());
		dto.setWriter(notice.getNoticeWriter());
		dto.setPostTime(notice.getNoticePostTime());
		// dto.setViews(notice.getViews());
		dto.setStatus(notice.isStatus());
		dto.setNoticeEditTime(notice.getNoticeEditTime());
		return dto;
	}

	// 수정
	@Transactional
	public NoticeDTO editNotice(Integer noticeId, NoticeDTO noticeDTO) {
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));

		notice.setNoticeTitle(noticeDTO.getTitle());
		notice.setNoticeContent(noticeDTO.getContent());

		notice.setStatus(noticeDTO.getStatus());
		Notice updated = noticeRepository.save(notice);

		NoticeDTO dto = new NoticeDTO();
		dto.setId(updated.getNoticeId());
		dto.setTitle(updated.getNoticeTitle());
		dto.setContent(updated.getNoticeContent());
		dto.setWriter(updated.getNoticeWriter());
		// dto.setViews(updated.getViews());
		dto.setStatus(updated.isStatus());
		dto.setNoticeEditTime(LocalDateTime.now());
		return dto;
	}

	// 삭제
	@Transactional
	public void deleteNotice(Integer noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
		notice.setStatus(false);
		notice.setNoticeDeleteTime(LocalDateTime.now());
		noticeRepository.save(notice);
	}
}

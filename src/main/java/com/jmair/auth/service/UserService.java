package com.jmair.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jmair.auth.dto.LoginDTO;
import com.jmair.auth.dto.Tokens;
import com.jmair.auth.dto.UserDTO;
import com.jmair.auth.dto.UserGrade;
import com.jmair.auth.entity.User;
import com.jmair.auth.repository.UserRepository;
import com.jmair.auth.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	// 회원가입
	public void join(UserDTO userDTO) {
		// 유저 ID가 이미 존재하는지 체크
		if (userRepository.existsByUserLogin(userDTO.getUserLogin())) {
			throw new IllegalArgumentException("이미 존재하는 회원입니다.");
		}

		// 비밀번호를 BCrypt 해싱하여 저장
		String encodedPassword = passwordEncoder.encode(userDTO.getPassword());

		User user = new User();
		user.setUserLogin(userDTO.getUserLogin());
		user.setUserName(userDTO.getUserName());
		user.setPassword(encodedPassword);
		user.setPhoneNumber(userDTO.getPhoneNumber());
		user.setEmail(userDTO.getEmail());
		user.setUserGrade(UserGrade.USER);
		user.setStatus(true);

		userRepository.save(user);
	}

	// 로그인 처리 - JWT 토큰 반환
	public Tokens login(LoginDTO loginDTO) {
		User user = userRepository.findByUserLogin(loginDTO.getUserLogin())
			.orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

		if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
		}

		String accessToken = jwtUtil.generateAccessToken(user);
		String refreshToken = jwtUtil.generateRefreshToken(user);

		return new Tokens(accessToken, refreshToken);
	}

	// 로그인한 사용자 정보 조회
	public User getUserByLogin(String userLogin) {
		return userRepository.findByUserLogin(userLogin)
			.orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
	}

}
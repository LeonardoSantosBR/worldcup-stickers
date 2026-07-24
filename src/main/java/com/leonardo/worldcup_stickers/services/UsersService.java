package com.leonardo.worldcup_stickers.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leonardo.worldcup_stickers.dto.MyProfileDto;
import com.leonardo.worldcup_stickers.dto.MyStickerDto;
import com.leonardo.worldcup_stickers.dto.PageResponseDto;
import com.leonardo.worldcup_stickers.entities.UserEntity;
import com.leonardo.worldcup_stickers.entities.UserStickerEntity;
import com.leonardo.worldcup_stickers.exceptions.EmailAlreadyExistsException;
import com.leonardo.worldcup_stickers.exceptions.UserNotFoundException;
import com.leonardo.worldcup_stickers.repositories.StickersRepository;
import com.leonardo.worldcup_stickers.repositories.UserStickersRepository;
import com.leonardo.worldcup_stickers.repositories.UsersRepository;

@Service
public class UsersService {
    private static final int MAX_LIMIT = 100;

    private final UsersRepository usersRepository;
    private final UserStickersRepository userStickersRepository;
    private final StickersRepository stickersRepository;
    private final HashService hashService;

    public UsersService(
            UsersRepository usersRepository,
            UserStickersRepository userStickersRepository,
            StickersRepository stickersRepository,
            HashService hashService) {
        this.usersRepository = usersRepository;
        this.userStickersRepository = userStickersRepository;
        this.stickersRepository = stickersRepository;
        this.hashService = hashService;
    }

    public boolean create(UserEntity user) {
        if (usersRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }
        
        String hashedPassword = hashService.hash(user.getPassword());
        user.setPassword(hashedPassword);
        usersRepository.save(user);
        return true;
    }

    @Transactional(readOnly = true)
    public MyProfileDto findMyUser(Long userId) {
        UserEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        long ownedStickers = userStickersRepository.countByUserId(userId);
        long totalStickers = stickersRepository.count();

        return MyProfileDto.fromEntity(user, ownedStickers, totalStickers);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<MyStickerDto> findMyStickers(Long userId, int page, int limit) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.min(Math.max(limit, 1), MAX_LIMIT),
                Sort.by("sticker.number").ascending());

        Page<UserStickerEntity> result = userStickersRepository.findByUserId(userId, pageable);
        return PageResponseDto.from(result, MyStickerDto::fromEntity);
    }
}

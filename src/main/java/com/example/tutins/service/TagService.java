package com.example.tutins.service;

import com.example.tutins.dto.TagDtos;
import com.example.tutins.entity.Tag;
import com.example.tutins.exception.BusinessException;
import com.example.tutins.exception.NotFoundException;
import com.example.tutins.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<TagDtos.TagResponse> findAll() {
        return tagRepository.findAll().stream().map(this::response).toList();
    }

    @Transactional
    public TagDtos.TagResponse create(TagDtos.TagRequest request) {
        if (tagRepository.findByNameIgnoreCase(request.name()).isPresent()) {
            throw new BusinessException("Такой тег уже существует");
        }
        Tag tag = new Tag();
        tag.setName(request.name().trim().toLowerCase());
        tagRepository.save(tag);
        log.info("Tag created id={} name={}", tag.getId(), tag.getName());
        return response(tag);
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new NotFoundException("Тег не найден: " + id));
        if (!tag.getCourses().isEmpty()) throw new BusinessException("Нельзя удалить тег, используемый в курсах");
        tagRepository.delete(tag);
        log.info("Tag deleted id={}", id);
    }

    private TagDtos.TagResponse response(Tag tag) {
        return new TagDtos.TagResponse(tag.getId(), tag.getName());
    }
}

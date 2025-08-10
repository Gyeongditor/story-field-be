package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;

public record MappedError(ErrorCode code, String message) { }

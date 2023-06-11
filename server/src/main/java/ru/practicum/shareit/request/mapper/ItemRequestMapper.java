package ru.practicum.shareit.request.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {
    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "requestorId", expression = "java(user)")
    @Mapping(target = "created", expression = "java(dateTime)")
    ItemRequest toItemRequest(ItemRequestAddDto itemRequestCreateDto, User user, LocalDateTime dateTime);

    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    @Mapping(target = "items", expression = "java(items)")
    ItemRequestExtendedDto toItemRequestExtendedDto(ItemRequest itemRequest, List<ItemDto> items);

}
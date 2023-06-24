package ru.practicum.main.compilation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.compilation.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    List<Compilation> findAllByPinned(boolean pinned, Pageable page);

    @Query(value = "select c.id as id, c.title as title, c.pinned as pinned, " +
            "e.id as id, e.title as title, e.annotation as annotation, " +
            "cat.id as id, cat.name as name, " +
            "e.description as description, e.paid as paid, e.participant_limit as participantLimit, " +
            "e.event_date as eventDate, e.created_on as createdOn, e.state as state, " +
            "e.published_on as publishedOn, u.name as name, u.id as id, u.email as email, " +
            "e.request_moderation as requestModeration " +
            "from compilation_event " +
            "left join events e on e.id = compilation_event.event_id " +
            "left join compilations c on c.id = compilation_event.compilation_id " +
            "left outer join categories cat on e.category_id = cat.id " +
            "left join users u on e.initiator_id = u.id " +
            "where c.pinned = :pinned ",
            nativeQuery = true)
    Page<Compilation> findAllByPinnedOrNot(@Param("pinned") boolean pinned, Pageable page);
}

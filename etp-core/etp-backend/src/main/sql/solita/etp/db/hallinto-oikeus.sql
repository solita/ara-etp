-- name: find-attachment-name-by-hallinto-oikeus-id
select attachment_name
from hallinto_oikeus
where id = :hallinto-oikeus-id;

-- name: find-document-template-wording-by-hallinto-oikeus-id
select document_template_wording_fi as fi, document_template_wording_sv as sv
from hallinto_oikeus
where id = :hallinto-oikeus-id;

-- update rakennustunnus to uppercase
update energiatodistus
set pt$rakennustunnus = upper(pt$rakennustunnus)
where pt$rakennustunnus != upper(pt$rakennustunnus);
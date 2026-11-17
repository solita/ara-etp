insert into a0_merkinta (id, label_fi, label_sv, valid, ordinal)
values
    (0, 'Ei', 'Ei (sv)', true, 1),
    (1, 'A0', 'A0', true, 2),
    (2, 'A0_korjattu', 'A0_korjattu', true, 3),
    (3, 'A0_MPR', 'A0_MPR', true, 4)
on conflict (id) do update set
    label_fi = excluded.label_fi,
    label_sv = excluded.label_sv,
    valid = excluded.valid,
    ordinal = excluded.ordinal;

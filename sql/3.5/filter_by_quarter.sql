WITH max_date AS (
    SELECT MAX(data) AS max_data
    FROM accounting.demonstracoes_contabeis
)
SELECT cp.razao_social,
       TO_CHAR(SUM(ad.vl_saldo_final), 'FM999,999,999,999,999.00') AS total_despesas
FROM accounting.demonstracoes_contabeis AS ad
         JOIN cadop.cadastro_operadoras AS cp
              ON ad.reg_ans::varchar = cp.registro_operadora
WHERE ad.descricao ILIKE '%EVENTOS/ SINISTROS CONHECIDOS OU AVISADOS  DE ASSISTÊNCIA A SAÚDE MEDICO HOSPITALAR%'
  AND DATE_TRUNC('quarter', ad.data) = DATE_TRUNC('quarter', (SELECT max_data FROM max_date))
GROUP BY cp.razao_social
ORDER BY SUM(ad.vl_saldo_final) DESC
LIMIT 10;
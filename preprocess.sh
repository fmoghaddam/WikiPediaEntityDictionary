cat log/positive.log | awk -F "\t" '{print $1"\t"$6"\t"$4}' > log/pos1Main.tsv
cat log/negativeDifficult.log | awk -F "\t" '{print $1"\t"$6"\t"$4}' > log/neg1Main.tsv

cat log/positive.log | awk -F "\t" '{print $1"\t"$6}' > log/pos2OnlyText.tsv
cat log/negativeDifficult.log | awk -F "\t" '{print $1"\t"$6}' > log/neg2OnlyText.tsv

awk 'NR > 5 { print }' < log/pos2OnlyText.tsv > log/pos3NoHeader.tsv 
awk 'NR > 5 { print }' < log/neg2OnlyText.tsv > log/neg3NoHeader.tsv 

cat log/pos3NoHeader.tsv | sort | uniq >  log/pos4Unique.tsv
cat log/neg3NoHeader.tsv | sort | uniq >  log/neg4Unique.tsv

#cat log/pos3NoHeader.tsv | uniq >  log/pos4Unique.tsv
#cat log/neg3NoHeader.tsv | uniq >  log/neg4Unique.tsv

awk 'length<1500' log/pos4Unique.tsv > log/pos5LengthLessThan1500.tsv
awk 'length<1500' log/neg4Unique.tsv > log/neg5LengthLessThan1500.tsv

head -15000 log/pos5LengthLessThan1500.tsv > log/15000Pos.tsv
head -15000 log/neg5LengthLessThan1500.tsv > log/15000Neg.tsv

grep -E 'MONARCH_TAG' log/15000Pos.tsv | head -125 >> log/500FinalPos.tsv
grep -E 'HEAD_OF_STATE_TAG' log/15000Pos.tsv | head -125 >> log/500FinalPos.tsv
grep -E 'POPE_TAG' log/15000Pos.tsv | head -125 >> log/500FinalPos.tsv
grep -E 'CHAIR_PERSON_TAG' log/15000Pos.tsv | head -125 >> log/500FinalPos.tsv


grep -E 'MONARCH_TAG' log/15000Neg.tsv | head -125 >> log/500FinalNeg.tsv
grep -E 'HEAD_OF_STATE_TAG' log/15000Neg.tsv | head -125 >> log/500FinalNeg.tsv
grep -E 'POPE_TAG' log/15000Neg.tsv | head -125 >> log/500FinalNeg.tsv
grep -E 'CHAIR_PERSON_TAG' log/15000Neg.tsv | head -125 >> log/500FinalNeg.tsv


cat log/500FinalPos.tsv | awk -F "\t" '{print $2}' > log/500FinalPos
cat log/500FinalNeg.tsv | awk -F "\t" '{print $2}' > log/500FinalNeg
